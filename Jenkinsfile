@Library('socrata-pipeline-library@9.6.1') _

commonPipeline(
  jobName: 'region-coder',
  language: 'scala',
  projects: [
    [
      name: 'region-coder',
      compiled: true,
      deploymentEcosystem: 'marathon-mesos',
      paths: [
        dockerBuildContext: 'docker',
      ],
      type: 'service'
    ]
  ],
  teamsChannelWebhookId: 'WORKFLOW_EGRESS_AUTOMATION',
)
